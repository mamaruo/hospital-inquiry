---
title: Sonner
description: An opinionated toast component for Vue.
component: true
links:
  doc: https://vue-sonner.vercel.app/
---

::component-preview
---
name: SonnerDemo
description: A sonner toast component.
---
::



## Usage

```vue showLineNumbers
<script setup lang="ts">
import { toast } from 'vue-sonner'
import { Button } from '@/components/ui/button'
</script>

<template>
  <Button @click="() => toast('My first toast')">
    Give me a toast
  </Button>
</template>
```

## Installation

The `<Toaster />` component and its specific CSS are required to display toasts. Add them to your root layout.

```vue showLineNumbers
<script setup lang="ts">
import 'vue-sonner/style.css'
import { Toaster } from '@/components/ui/sonner'
</script>

<template>
  <div>
    <main>
      <!-- Your app content -->
    </main>
    <Toaster />
  </div>
</template>
```

## Examples

### Types

```vue
<!-- eslint-disable no-console -->
<!-- eslint-disable no-template-curly-in-string -->
<script setup lang="ts">
import { toast } from 'vue-sonner'

import { Button } from '@/components/ui/button'

function handlePromiseClick() {
  toast.promise<{ name: string }>(
    () =>
      new Promise(resolve =>
        setTimeout(() => resolve({ name: 'Event' }), 2000),
      ),
    {
      loading: 'Loading...',
      success: (data: { name: string }) => `${data.name} has been created`,
      error: 'Error',
    },
  )
}
</script>

<template>
  <div class="flex flex-wrap gap-2">
    <Button variant="outline" @click="() => toast('Event has been created')">
      Default
    </Button>
    <Button
      variant="outline"
      @click="() => toast.success('Event has been created')"
    >
      Success
    </Button>
    <Button
      variant="outline"
      @click="() =>
        toast.info('Be at the area 10 minutes before the event time')
      "
    >
      Info
    </Button>
    <Button
      variant="outline"
      @click="() =>
        toast.warning('Event start time cannot be earlier than 8am')
      "
    >
      Warning
    </Button>
    <Button
      variant="outline"
      @click="() => toast.error('Event has not been created')"
    >
      Error
    </Button>
    <Button
      variant="outline"
      @click="handlePromiseClick"
    >
      Promise
    </Button>
  </div>
</template>
```

### With Dialog

```vue
<script setup lang="ts">
import { toast } from 'vue-sonner'
import { Button } from '@/components/ui/button'
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from '@/components/ui/dialog'
</script>

<template>
  <Dialog>
    <DialogTrigger as-child>
      <Button variant="outline">
        Dialog with toast
      </Button>
    </DialogTrigger>
    <DialogContent
      class="sm:max-w-md"
      @interact-outside="event => {
        const target = event.target as HTMLElement;
        if (target?.closest('[data-sonner-toaster]')) return event.preventDefault()
      }"
    >
      <DialogHeader>
        <DialogTitle>Vue Sonner Toast</DialogTitle>
        <DialogDescription> Dialog with toast </DialogDescription>
      </DialogHeader>
      <div class="grid gap-4">
        <Button
          size="sm"
          class="px-3"
          @click="
            () => {
              toast('Event has been created', {
                description: 'Sunday, December 03, 2023 at 9:00 AM',
                action: {
                  label: 'Undo',
                  onClick: () => console.log('Undo'),
                },
              });
            }
          "
        >
          Toast
        </Button>
      </div>
    </DialogContent>
  </Dialog>
</template>
```
